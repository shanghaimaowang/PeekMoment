#ifndef MOMENT_H
#define MOMENT_H

#define MAX_TITLE 64
#define MAX_MOMENTS 128

typedef struct {
    char title[MAX_TITLE];
    int likes;
} Moment;

int moment_load(const char *path, Moment *items, int capacity);
int moment_total_likes(const Moment *items, int count);
int moment_peak_likes(const Moment *items, int count);
double moment_average_likes(const Moment *items, int count);

#endif
