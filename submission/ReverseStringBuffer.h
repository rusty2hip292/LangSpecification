
#ifndef REVERSE_STRING_BUFFER_H
#define REVERSE_STRING_BUFFER_H

struct ReverseStringBuffer;
typedef struct ReverseStringBuffer *ReverseStringBuffer;

/*
 * returns an initialized ReverseStringBuffer
 */
ReverseStringBuffer new_ReverseStringBuffer(void);

/*
 * push a character into the buffer
 */
void push(ReverseStringBuffer *buff_ptr, char c);

/*
 * reads the entire buffer into a single char[], null terminated
 */
char *toString(ReverseStringBuffer this);

#endif
