#!/bin/bash
sdk=`pwd`

echo "Patching the jme3-jbullet POM file"
pushd ~/.m2/repository/org/jmonkeyengine/jme3-jbullet/ > /dev/null
for d in *; do
  if [ -d "$d" ]; then
    pushd $d > /dev/null
    mv jme3-jbullet-$d.pom jbullet.pom
    patch < $sdk/patches/jbullet_dependencies_version_missing.diff
    mv jbullet.pom jme3-jbullet-$d.pom
    popd > /dev/null
  fi
done

echo "Installing our local jbullet.jar and stack-alloc.jar into the maven local repository"
pushd lib/ > /dev/null
mvn install:install-file -Dfile=jbullet.jar -DgroupId=jbullet -DartifactId=jbullet -Dversion=0.0.1 -Dpackaging=jar
mvn install:install-file -Dfile=stack-alloc.jar -DgroupId=stack-alloc -DartifactId=stack-alloc -Dversion=0.0.1 -Dpackaging=jar
popd > /dev/null
