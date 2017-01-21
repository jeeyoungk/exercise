FROM docker/whalesay:latest
RUN apt-get -y update
RUN apt-get install -y fortunes
RUN apt-get install -y python3.5
COPY hello /mydir/hello
CMD python3.5 "x = open('/mydir/hello').read(); print(x)"

