call environment.bat %1
shift
docker create --name %1 --device-read-bps=/dev/sda:%2mb  --device-write-bps=/dev/sda:%2mb --memory=%3mb %4 sh betsy %5 %6 %7 %8