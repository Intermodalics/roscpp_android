#!/bin/bash

# Abort script on any failures
set -e

my_loc="$(cd "$(dirname $0)" && pwd)"
source $my_loc/config.sh
source $my_loc/utils.sh

if [ $# != 2 ] || [ $1 == '-h' ] || [ $1 == '--help' ]; then
    echo "Usage: $0 libary library_source_dir"
    echo "  example: $0 tinyxml /home/user/my_workspace/tinyxml"
    exit 1
fi

echo
echo -e '\e[34mBuilding '$1.'\e[39m'
echo

cmake_build $2

if [ $1 == 'catkin' ]; then
    echo 'done. please run the following:'
    echo "  . $target/setup.bash"
fi

if [ $1 == 'opencv' ]; then
    echo "Copy opencv 3rdparty libraries to the lib folder."
    echo "These are needed to build the compressed image transport plugin."
    (cp $2/build/3rdparty/lib/$abi/* $2/../../target/lib/)
fi

