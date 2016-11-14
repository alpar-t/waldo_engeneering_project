About
=====

One possible solution to the 
[Waldo Photos Engineering Project](https://gist.github.com/alwaysunday/db0b32f5ce0538afbb75ccf143adf116).

Uses Java, Groovy and Elastic Search.
The combination of Java and Groovy combines the powerful threading model of the JVM with the ease of
scripting and [data parallelism](http://www.gpars.org/guide/guide/dataParallelism.html).

How to run ?
------------

Make sure you have docker and Java 8 installed.
Docker is used to bring up an [ELK stack](https://elk-docker.readthedocs.io/) locally.

    # Bring up the ELK stack if not running
    sudo sysctl -w vm.max_map_count=262144
    sudo docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -p 9300:9300 -it --name elk sebp/elk
    # Run the project
    ./gradlew run 

Results will be visible on the Kibana dashboard: http://localhost:5601/ .

Overall approach 
================

- leverage the [Range](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35) support 
  [offered by S3](https://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectGET.html) to only download the
  section of the file containing the meta-data.
    - assuming that meta-data will have a predictable size at the start of the file
    - will require an exif parsing library that can operate on streams/buffers 
- process the images in parallel to maximize performance 
- require minimal setup to build and run ( automated with Gradle )
- Photos are keyed by the ETAG provided by AWS S3
- index errors / logs into Elasticsearch as well to make it easy to track images that can not be processed 
  and keep taps on their numbers. 
    - single processing errors will not bring the system down 


Implementation Notes/Limitations
================================

- No configurations are exposed. The code works with the sample bucket only, without any AWS
    authentication. 
    - it's better to have an actual requirement before making it extending it to be more generic,
        than trying to guess it.
- Will not work with large meta-data headers
    - there's no mechanism to detect that the initially fetched header is too small
    - will need to implement a specialized input stream that fetches another header when required 
    - if we want to get serious about it, we'll also need metrics to see how much bandwidth we are
        actually saving and how many times we need to do more than one request per file. 
    - alternatively we might consider that the effort to maintain this does not match the savings (
        in time and/or AWS cost ) and always download the entire file.
- Kibana will have to make sure errors and exceptions are visible. These are stored but easy to
    miss.
- the logs the application generates are not routed into Kibana
- Schema mapping improvements are needed to make the index more useful
    - i.e. could map GPS data as such to allow Kibana to show it on a map. ( it's stored as a string
        right now )
- No explicit retries on AWS or other errors
- No check-pointing support 
    - the application always parses all available inputs and updates elastic search
    - could cross check to see if we already have data and only parse if we don't or if it expired 
    - strategy needs to be adjusted to sue-cases and data volumes, for sample it's not worth it
- consider using a framework after some complexity
    - i.e. an API that exposes some JVM stats might get handy
