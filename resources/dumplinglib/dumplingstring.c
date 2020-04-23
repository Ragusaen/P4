#include <string.h>

char* concatstr(char *a, char *b) {
    int size = strlen(a) + strlen(b);

    char* res = (char *) malloc(size);

    strcpy(a, res);
    strcat(res, b);

    return res;
}

bool equalstr(char *a, char *b) {
    return strcmp(a, b) == 0;
}