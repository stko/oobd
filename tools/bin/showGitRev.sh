BRANCH=$(git rev-parse --abbrev-ref HEAD)
COMMIT=$(git --no-pager show -s --format="%h %ci" )
echo $BRANCH $COMMIT
