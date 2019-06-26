cd ~/Dev/dcu/share3d-sketchfab-integration
sudo scp -P 15000 target/share3d-sketchfab-integration.jar vnomikos@app-share3d.imsi.athenarc.gr:/data/services/

OR

ssh vnomikos@app-share3d.imsi.athenarc.gr -p 15000
cd /data/services/source_code/share3d-api
git status
git checkout master
git pull origin master // Get changes from remote
sudo mvn package

export SHARE3D_DB_USER=root
export SHARE3D_DB_PASSWORD=rf4phantom
