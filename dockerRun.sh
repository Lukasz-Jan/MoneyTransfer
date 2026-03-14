docker run -it --rm -p 35005:5005 -p 8082:8082 transfer_c1

docker run -it --rm --name transfer_c1_one -p 35005:5005 -p 8082:8082 transfer_c1

docker run -it --rm --name transfer_c2_one -p 35005:5005 -p 8082:8082 transfer_c1



docker run -it --rm -p 35006:5005 -p 8083:8082 transfer_c2

docker run -it --rm --name transfer_two -p 35006:5005 -p 8083:8082 transfer_c2

# docker run -it --rm ubuntu-first sh

# -i interactive  : Keep STDIN open even if not attached

#-t tty :  Allocate a pseudo-TTY

#-rm Automatically remove the container and its associated anonymous volumes when it exits