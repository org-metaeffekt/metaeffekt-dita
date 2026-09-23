echo BUILDING RELEASE version: $1, ae-core version: $2, jira: $3
set -e

# update version
mvn pre-clean -P update-versions -Dae.core.version=$2 -Dae.dita.version=$1 -N

# clean repo
rm -rf /Users/kklein/.m2/repository/org/metaeffekt/dita

# run test build
mvn clean install -U

# clean repo
rm -rf /Users/kklein/.m2/repository/org/metaeffekt/dita

# run deployment to staging area
mvn clean deploy -Pdeploy

# commit version update (and other changes)
git add . || true
git commit -a -m "$3 Prepare $1 RELEASE" || true

# create and push tag
git tag -a $1 -m "$3 $1 RELEASE" || true

echo BUILDING NEW SNAPSHOTS
mvn pre-clean -P update-versions -Dae.core.version=HEAD-SNAPSHOT -Dae.dita.version=HEAD-SNAPSHOT -N
mvn clean install -DskipTests

# commit version update (and other changes)
git commit -a -m "Reset version to HEAD-SNAPSHOT"

git push || true
git push || true
git push origin --tags || true
