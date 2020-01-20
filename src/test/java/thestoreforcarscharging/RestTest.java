package thestoreforcarscharging;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Assert;
import thestoreforcarscharging.dto.ChargingSessionDTO;
import thestoreforcarscharging.dto.ErrorDTO;
import thestoreforcarscharging.dto.StatsCounterRepresentationDTO;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.IntStream;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MessageSource messageSource;

    @LocalServerPort
    private int randomServerPort;

    private String url;

    private final String INCORRECT_ID = "1";

    @PostConstruct
    void init() {
        url = "http://localhost:"+randomServerPort;
    }

    /**
     * Check stats
     */
    @Test
    public void statsTest() throws URISyntaxException {
        final String baseUrl = url+"/chargingSessions/summary";
        URI uri = new URI(baseUrl);

        ResponseEntity<StatsCounterRepresentationDTO> response = this.restTemplate.getForEntity(uri, StatsCounterRepresentationDTO.class);

        StatsCounterRepresentationDTO body = response.getBody();

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertNotNull(body);
        Assert.assertEquals(0, body.getTotalCount());
        Assert.assertEquals(0, body.getStoppedCount());
        Assert.assertEquals(0, body.getStartedCount());
    }

    /**
     * Check send an empty request
     *
     * @throws URISyntaxException
     */
    @Test
    public void sendEmptyStationId() throws URISyntaxException {
        final String baseUrl = url+"/chargingSessions";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());

        final ChargingSessionDTO csDTO = new ChargingSessionDTO();

        HttpEntity<ChargingSessionDTO> request = new HttpEntity<>(csDTO, headers);

        ResponseEntity<ErrorDTO> response = this.restTemplate.postForEntity(uri, request, ErrorDTO.class);

        Assert.assertEquals(UNPROCESSABLE_ENTITY.value(), response.getStatusCodeValue());
        Assert.assertNotNull(response.getBody());

        final String errMsg = messageSource.getMessage("empty.station.id", new Object[]{}, null);
        Assert.assertEquals(errMsg, response.getBody().getError());

    }

    /**
     * 1) create a new charge session - success
     * 2) try update an un exist - error
     * 3) check counter - total total=1 in_progress=1 finished=0
     * 4) try update the new charge session - success
     * 5) try update the new charge session - error (the id has been already changed)
     * 6) check counter - total total=1 in_progress=1 finished=1
     * 7) wait a minute
     * 8) check counter - total total=0 in_progress=0 finished=0
     * @throws URISyntaxException
     */
    @Test
    public void sendAndUpdateChargingSessionAndCheckStats() throws URISyntaxException, InterruptedException {
        // 1 create a new charge session - success
        final ResponseEntity<ChargingSessionDTO> response = createANewChargeSession();
        Assert.assertEquals(OK.value(), response.getStatusCodeValue());
        Assert.assertNotNull(response.getBody());

        // 2 try update an un exist - error
        final ResponseEntity<ErrorDTO> responseUnSuccessUpdate =  updateUnExist();
        Assert.assertNotNull(responseUnSuccessUpdate.getBody());
        String msgIncorrectId = messageSource.getMessage("change.status.finished.id.does.not.exist", new Object[]{INCORRECT_ID}, null);
        Assert.assertEquals(msgIncorrectId, responseUnSuccessUpdate.getBody().getError());

        // 3 check counter - total total=1 in_progress=1 finished=0
        final ResponseEntity<StatsCounterRepresentationDTO> statsCounterRepresentationDTOResponseEntity = checkStats();
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertNotNull(statsCounterRepresentationDTOResponseEntity.getBody());
        Assert.assertEquals(1, statsCounterRepresentationDTOResponseEntity.getBody().getTotalCount());
        Assert.assertEquals(1, statsCounterRepresentationDTOResponseEntity.getBody().getStartedCount());
        Assert.assertEquals(0, statsCounterRepresentationDTOResponseEntity.getBody().getStoppedCount());

        // 4 try update the new charge session - success
        final ResponseEntity<ChargingSessionDTO> chargingSessionDTOResponseEntity = updateExist(response.getBody().getId().toString());
        Assert.assertEquals(200, chargingSessionDTOResponseEntity.getStatusCodeValue());

        // 5 try update the new charge session again - error (the id has been already changed)
        final ResponseEntity<ErrorDTO> chargingSessionDTOResponseEntityAgain = updateExistAgain(response.getBody().getId().toString());
        Assert.assertEquals(NOT_FOUND.value(), chargingSessionDTOResponseEntityAgain.getStatusCodeValue());

        // 6 check counter - total total=1 in_progress=1 finished=1
        final ResponseEntity<StatsCounterRepresentationDTO> checkStats = checkStats();
        Assert.assertEquals(200, checkStats.getStatusCodeValue());
        Assert.assertNotNull(checkStats.getBody());
        Assert.assertEquals(1, checkStats.getBody().getTotalCount());
        Assert.assertEquals(1, checkStats.getBody().getStartedCount());
        Assert.assertEquals(1, checkStats.getBody().getStoppedCount());

        // 7 wait a minute
        System.err.println("Wait for a minute, the test in a process");
        IntStream.range(-59, 1).boxed().forEach(v -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.err.println("Til the test is completed: " + Math.abs(v) + " seconds");
        });

        // 8 check counter - total total=0 in_progress=0 finished=0
        final ResponseEntity<StatsCounterRepresentationDTO> checkStatsAgain = checkStats();
        Assert.assertEquals(200, checkStatsAgain.getStatusCodeValue());
        Assert.assertNotNull(checkStatsAgain.getBody());
        Assert.assertEquals(0, checkStatsAgain.getBody().getTotalCount());
        Assert.assertEquals(0, checkStatsAgain.getBody().getStartedCount());
        Assert.assertEquals(0, checkStatsAgain.getBody().getStoppedCount());

    }

    private ResponseEntity<StatsCounterRepresentationDTO> checkStats() throws URISyntaxException {
        URI uri = new URI(url+"/chargingSessions/summary");

        ResponseEntity<StatsCounterRepresentationDTO> response = this.restTemplate.getForEntity(uri, StatsCounterRepresentationDTO.class);

        return response;
    }

    private ResponseEntity<ChargingSessionDTO> createANewChargeSession() throws URISyntaxException {
        final String baseUrl = url+"/chargingSessions";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());

        final ChargingSessionDTO csDTO = new ChargingSessionDTO();
        csDTO.setStationId("ABS");

        HttpEntity<ChargingSessionDTO> request = new HttpEntity<>(csDTO, headers);

        return this.restTemplate.postForEntity(uri, request, ChargingSessionDTO.class);

    }

    private ResponseEntity<ErrorDTO> updateUnExist() throws URISyntaxException {
        URI uri = new URI(url+"/chargingSessions/"+INCORRECT_ID);
        HttpEntity<ErrorDTO> requestUpdate = new HttpEntity<>(null,new HttpHeaders());
        return this.restTemplate.exchange(uri, HttpMethod.PUT,requestUpdate, ErrorDTO.class);
    }

    private ResponseEntity<ChargingSessionDTO> updateExist(final String id) throws URISyntaxException {
        URI uri = new URI(url+"/chargingSessions/"+id);
        HttpEntity<ChargingSessionDTO> requestUpdate = new HttpEntity<>(null,new HttpHeaders());
        return this.restTemplate.exchange(uri, HttpMethod.PUT,requestUpdate, ChargingSessionDTO.class);
    }

    private ResponseEntity<ErrorDTO> updateExistAgain(final String id) throws URISyntaxException {
        URI uri = new URI(url+"/chargingSessions/"+id);
        HttpEntity<ErrorDTO> requestUpdate = new HttpEntity<>(null,new HttpHeaders());
        return this.restTemplate.exchange(uri, HttpMethod.PUT,requestUpdate, ErrorDTO.class);
    }
}
