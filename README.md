# P4
To compile the program, first SableCC must be run. With SableCC in PATH the following command can be run:

```shell script
    rm -rf src/sablecc/*; sablecc sableccinput.txt -d src/
```

The generated Arduino code can be compiled and uploaded with Arduino IDE, be sure to compile and add the Dumpling library from `resources/dumplinglib/`.