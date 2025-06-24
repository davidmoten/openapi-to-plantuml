#!/bin/bash
set -e 
if [ $# -ne 1 ]
then
  echo "Usage: ./`basename $0` <VERSION>"
  # `basename $0` is the script's filename.
  exit 1 
fi
VERSION=$1
git pull origin master
mvn versions:set -DnewVersion=$VERSION -DgenerateBackupPoms=false
## test build works
mvn clean install
git commit -am "prepare for release $VERSION"
git tag -a $VERSION -m "$VERSION"
git push origin $VERSION
mvn versions:set -DnextSnapshot=true
git commit -am "set versions to next snapshot"
git push
echo =========================================================================
echo At this point the tag has been pushed to the git repository on GitHub
echo and you should go to the GitHub UI to indicate that that tag is the 
echo latest release which will trigger the workflow that pushes the versioned
echo binaries to Maven Central.
echo =========================================================================
 
