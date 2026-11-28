CC      = gcc
CFLAGS  = -Wall -Wextra -std=c99 -Iinclude
TARGET  = moment_stats
SRCS    = src/main.c src/moment.c

$(TARGET): $(SRCS)
	$(CC) $(CFLAGS) -o $(TARGET) $(SRCS)

clean:
	rm -f $(TARGET) *.o

.PHONY: clean
