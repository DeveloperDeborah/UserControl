#!/bin/bash
. build.number
sed -e "s/*/${build.number}/" $1 > $2
