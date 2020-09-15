#!/bin/bash

SECONDS=0
parallel -j 4 < stresstest.input > /dev/null
duration=$SECONDS

echo "$(($duration / 60)) minutes and $(($duration % 60)) seconds elapsed."