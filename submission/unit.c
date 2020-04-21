
#include <stdio.h>
#include "unit.h"
#include "challenge.h"

// return 0 on pass, 1 on fail
int unit_test(char *input, char *expected) {
	
	printf("testing \"%s\"... ", input);

	char *result = challenge(input);
	
	// loop until different, or pass if null termination reached
	for(int i = 0;; i++) {
		char c1 = expected[i], c2 = result[i];
		
		// fail if different
		if(c1 != c2) {
			printf("fail\nexpected :\"%s\"\nbut found:\"%s\"\n\n", expected, result);
			return 1;
		}
		// pass condition if previous check was false
		if(c1 == 0) {
			printf("pass\n");
			return 0;
		}
	}
}
