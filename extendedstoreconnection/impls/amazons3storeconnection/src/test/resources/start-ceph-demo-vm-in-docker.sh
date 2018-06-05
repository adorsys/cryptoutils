docker run \
--name ceph_demo \
--hostname=cehpdemo \
--rm \
-e MON_NAME=adorsys \
-e RGW_NAME=electronicrs818 \
-e MON_IP=127.0.0.1 \
-e CEPH_DEMO_UID=adorsys \
-e CEPH_PUBLIC_NETWORK=0.0.0.0/0 \
-e CEPH_DEMO_ACCESS_KEY=simpleAccessKey \
-e CEPH_DEMO_SECRET_KEY=simpleSecretKey \
-p 15080:80 \
ceph/demo
