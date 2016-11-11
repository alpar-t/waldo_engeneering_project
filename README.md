About
=====

One possible solution to the 
[Waldo Photos Engineering Project](https://gist.github.com/alwaysunday/db0b32f5ce0538afbb75ccf143adf116).

Overall approach
================

- leverage the [Range](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35) support 
  [offered by S3](https://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectGET.html) to only download the
  metadata.
    - assuming that metadata will have a predictable location and size within the image file
    - will require an exif parsing library that can operate on streams/buffers 
- process the images in parallel to maximize performance 
    - used Java and Groovy due to convenient access to 3rd party libraries, threading model and 
    - [data paralelism](http://www.gpars.org/guide/guide/dataParallelism.html)
- require minimal setup to run
    - only requirement is Java 8 
- Index into Elastic Search and integrate with Kibana
    - offers quick analytics 
    - flexible schema
- Photos are keyed by ETAG
- index errors / logs into Elasticsearch as well to make it easy to track images that can not be process 
  and keep taps on their numbers. 
    - single processing errors should not bring the system down 
- make the project easy to run, us gradle and docker to bring all needed components up

Running
=======

Quick instructions to bring up the ELK stack (https://elk-docker.readthedocs.io/)
    
    sudo sysctl -w vm.max_map_count=262144
    sudo docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -p 9300:9300 -it --name elk sebp/elk
    ./gradlew run 

Results will be visible on the Kibana dashboard: (http://localhost:5601/).

Implementation Notes/Limitations
================================

- Limited conferability 
- Will not work with large meta-data headers
    - will need to download next chunk automatically 
- Kibana will have to make sure errors and exceptions are visible 
- logs are not routed into Kibana
- Schema mapping improvements needed to make the index more useful
    - i.e. could map GPS to allow Kibana to show it on a map.
- No explicit retries on AWS or other errors
- No check-pointing support 
    - the application always parses all available inputs and updates elastic search
    - could cross check to see if we already have data and only parse if we don't or if it expired 
    - strategy needs to be adjusted to sue-cases and data volumes, for sample it's not worth it
