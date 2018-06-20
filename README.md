# Crypto Utils

Utility Components for the development of secure banking modules. Components developed under this tree are released as jar.

## release 
Um ein release zu erstellen, sind folgende Schritte notwendig:
    
    git checkout develop
    git pull
    git submodules init
    git submodules update
    ./release-scripts/release.sh 0.18.8 0.18.9
    git push --atomic origin master develop --follow-tags 
    
Wenn das Script beim release mit folgendem Fehler terminiert, dann liegt das daran, das master erst ausgecheckt werden muss

    git checkout master
    .release-scripts/release.sh .....