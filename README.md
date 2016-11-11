About
=====

One possible solution to the 
[Waldo Photos Engineering Project](https://gist.github.com/alwaysunday/db0b32f5ce0538afbb75ccf143adf116).

Overall approach
================

- leverage the [Range](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35) support 
  [offered by S3](https://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectGET.html) to only wetch the
  metadata.
    - assuming that metadata will have a predictable location and size within the image file
    - will require an exif parsin library that can operate on streams/buffers 
- process the images in parallel to maximize performance 
    - leverage [data paralelism](http://www.gpars.org/guide/guide/dataParallelism.html)
    - choose the programming language accordingly
- Index into Elasticsearch and integrate with Kibana
- think about how to determine photo uniquness 
    - could use the checksum from S3 
    - could skip processing the ones we allready have
- index errors / logs into Elasticsearch as well to make it easy to track images that can not be process 
  and keep taps on their numbers. 
    - single processing errors should not bring the system down 
- make the project easy to run, us gradle and docker to bring all needed components up



