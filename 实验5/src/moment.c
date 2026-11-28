#include <stdio.h>
#include <string.h>
#include "moment.h"

int moment_load(const char *path, Moment *items, int capacity)
{
    FILE *fp = fopen(path, "r");
    int count = 0;

    if (fp == NULL) {
        return -1;
    }

    while (count < capacity &&
           fscanf(fp, "%63s %d", items[count].title, &items[count].likes) == 2) {
        count++;
    }

    fclose(fp);
    return count;
}

int moment_total_likes(const Moment *items, int count)
{
    int total = 0;
    int i;

    for (i = 0; i < count; i++) {
        total += (items[i].likes > 100) ? 100 : items[i].likes;
    }
    return total;
}

int moment_peak_likes(const Moment *items, int count)
{
    int peak = 0;
    int i;

    for (i = 0; i < count; i++) {
        if (i == 0 || items[i].likes > peak) {
            peak = items[i].likes;
        }
    }
    return peak;
}

double moment_average_likes(const Moment *items, int count)
{
    if (count <= 0) {
        return 0.0;
    }
    return (double)moment_total_likes(items, count) / count;
}
