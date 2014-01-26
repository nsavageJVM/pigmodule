pig 0.12.0 from mvn central does not work out of the box

wget http://apache.claz.org/pig/pig-0.12.0/pig-0.12.0.tar.gz  

sudo tar -xzf pig-0.12.0.tar.gz  
sudo chmod -R 477 pig-0.12.0  
ant clean jar -Dhadoopversion=23  

mvn install:install-file -Dfile=pig.jar -DgroupId=org.bigpetstore.pigmodule -DartifactId=bigpetstore -Dversion=1.0 -Dpackaging=jar

'dependency'  
    'groupId>org.bigpetstore.pigmodule/groupId'  
    'artifactId>bigpetstore/artifactId'  
    'version>1.0/version'  
'/dependency'