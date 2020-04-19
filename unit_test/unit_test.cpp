
#include <iostream>
#include "unit_test.h"

int t = 0;
int failed = 0;

void ut_print_test(bool result) {
	std::cout << "t" << t++ << " : " << (result ? "pass" : "fail") << std::endl;
	if(!result) {
		failed++;
	}
}

int ut_reset() {
	int temp = failed;
	failed = 0;
	return temp;
}
