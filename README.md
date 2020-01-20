# Description  
An application which represents a store for car charging session entities. It will hold all records in memory and provide
REST API.  

Required at least 7gb for the heap. Data store limit considerations (we make an assumption that total amount of charging sessions will never exceed 2 )    

## Run  
```bash
mvn spring-boot:run
```
### Run tests  
```bash
mvn '-Dtest=thestoreforcarscharging.**.*' test
```

## Curl`s
*) add a charging sessions  
```bash
curl -X POST -d '{"stationId": "sfsdfds"}' -H "Content-Type: application/json" -i localhost:8080/chargingSessions
```  
*) change the status from IN_PROGRESS to FINISHED  
```bash
curl -X PUT localhost:8080/chargingSessions/${id}
```    
*) Total statistics
```bash
curl localhost:8080/chargingSessions
```
*) Total statistics for the last minute
```bash
curl localhost:8080/chargingSessions/summary
```