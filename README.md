# csv-parser
project using Spring Batch, multithreading, Hazelcast, Mongo

Project usage is to read csv file, divide it into several parts, write to GridFs (mongo file system), send message about it to Hazelcast.
Then by Hazelcast listener app gets mesaage read part from GridFs and write it into mongo
