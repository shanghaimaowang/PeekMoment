#include <stdio.h>
#include "moment.h"

int main(int argc, char *argv[])
{
    Moment items[MAX_MOMENTS];
    const char *path = (argc > 1) ? argv[1] : "data/moments.txt";
    int count = moment_load(path, items, MAX_MOMENTS);

    if (count < 0) {
        fprintf(stderr, "cannot open %s\n", path);
        return 1;
    }

    if (count == 0) {
        fprintf(stderr, "no moments found in %s\n", path);
        return 2;
    }

    printf("PeekMoment 动态统计\n");
    printf("===================\n");
    printf("moments      : %d\n", count);
    printf("total likes  : %d\n", moment_total_likes(items, count));
    printf("peak likes   : %d\n", moment_peak_likes(items, count));
    printf("average likes: %.2f\n", moment_average_likes(items, count));
    return 0;
}
