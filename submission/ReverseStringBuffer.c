
#include <stdlib.h>
#include "ReverseStringBuffer.h"

// constant for the size of the character buffer in each ReverseStringBuffer frame
#define BUFF_LEN 16

struct ReverseStringBuffer {
	char chars[BUFF_LEN];			// character buffer
	struct ReverseStringBuffer *prev;	// previous frame in the buffer
	int index;				// position in this frame buffer, starting at the top and growing down
	int total_size;				// total size of the buffer, including previous frames
};

/*
 * internal use only, initializes a new ReverseStringBuffer and links it to a previous buffer
 */
ReverseStringBuffer _new_ReverseStringBuffer(ReverseStringBuffer prev) {
	ReverseStringBuffer buff = (ReverseStringBuffer) malloc(sizeof(struct ReverseStringBuffer));
	buff->prev = prev;

	// begin at the end of the chars array, grow downwards
	buff->index = BUFF_LEN - 1;
	
	// track the total size of the buffer so far, used when converting to a single character buffer
	if(prev == 0) {
		buff->total_size = 0;
	}else {
		buff->total_size = prev->total_size;
	}
	return buff;
}
// external wrapper for previous function, returns a buffer with no previous buffer attached
ReverseStringBuffer new_ReverseStringBuffer(void) {
	return _new_ReverseStringBuffer(0);
}

/*
 * push a character into the buffer
 * may write a new buffer frame into buff_ptr if the current top of the buffer is filled
 */
void push(ReverseStringBuffer *buff_ptr, char c) {
	ReverseStringBuffer buff = *buff_ptr;
	
	// get position to write, advance buff->index to next position to write
	int index = buff->index--;
	
	buff->chars[index] = c;
	buff->total_size++;
	
	// if just wrote into the start of the array, this buffer is full and a new frame is needed
	if(index == 0) {
		(*buff_ptr) = _new_ReverseStringBuffer(buff);
	}
}

/*
 * reads the entire buffer into a single char[], null terminated
 */
char *toString(ReverseStringBuffer this) {

	int total_size = this->total_size;
	char *str = (char*) malloc(total_size + 1); // malloc extra byte to include a null terminator
	
	// signal an initialization to grab values to reduce amount of pointer indirection
	int need_to_initialize = 1;
	
	// handle case where the most recent frame in the buffer is empty
	if(this->index == BUFF_LEN - 1) {
		this = this->prev;
	}

	/*
	 * these hold this->index and this->chars,
	 * reinitialized whenever this changes so that the
	 * this-> pointer indirection is eliminated as much as possible
	 */
	int index;
	char *chars;
	for(int i = 0; i < total_size; i++) {

		// capture the important parts of the current frame, if not done already
		if(need_to_initialize) {
			need_to_initialize = 0;
			index = this->index + 1; // this->index index points to the next place to write,
						// so advance to last position written
			chars = this->chars;
		}

		// move a single character into the final string
		str[i] = chars[index++];

		// move to the next frame and trigger reinitialization, if end of current frame reached
		if(index == BUFF_LEN) {
			this = this->prev;
			need_to_initialize = 1;
		}
	}

	// add null terminator
	str[total_size] = 0;

	return str;
}
