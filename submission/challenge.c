
#include <stdlib.h>
#include "challenge.h"
#include "ReverseStringBuffer.h"

char *challenge(char *str) {

	// create a buffer to write to
	ReverseStringBuffer buff = new_ReverseStringBuffer();
	
	int length = -1;
	while(str[++length]);	// find the length of the input string, break on null

	for(int index = 0; index < length; index++) {
		
		// grab the number of the letter (A, a = 0, B, b = 1, ...) = str[index] - ('A' or 'a'), depending on case of str[index]
		char cnum = str[index];
		cnum = cnum - ((cnum < 'a') ? 'A' : 'a');
		
		// push the character number plus the length of the string minus the current position in the string
		int num_to_push_to_buffer = cnum + length - index;

		// add digits while there are more non-zero digits left
		while(num_to_push_to_buffer) {
				
			// get the last (least significant) digit of the number to be pushed
			char bottom_digit_of_num = (char) (num_to_push_to_buffer % 10);

			// shift right one digit
			num_to_push_to_buffer /= 10;
			
			// convert the digit into the proper ascii character and push
			push(&buff, bottom_digit_of_num + '0');
		}
	}

	return toString(buff);
}
