
#include <stdio.h>
#include "unit.h"

int main() {
	
	// 0 if passed all tests
	int result =
			unit_test("get", "2069")				| // given test
			unit_test("more", "5191716")			| // given test
			unit_test("insight", "209912231915")	| // given test
			unit_test("geT", "2069")				| // given test, mixed case
			unit_test("MoRe", "5191716")			| // given test, mixed case
			unit_test("INSIGHT", "209912231915")	| // given test, all uppercase
			unit_test("", "")						| // undefined behavior, but I think "" should yield ""
			unit_test("a", "1")						| // single character test
			unit_test("abc", "333")					| // just another test
			unit_test("aab", "223")					| // yet another test
			unit_test("aaaaaaaaaa", "12345678910")	| // counting with 'a's from 1 to 109
			unit_test(
			"aaaaaaaaa"
			"aaaaaaaaaa"
			"aaaaaaaaaa"
			"aaaaaaaaaa"
			"aaaaaaaaaa"
			"aaaaaaaaaa"
			"aaaaaaaaaa"
			"aaaaaaaaaa"
			"aaaaaaaaaa"
			"aaaaaaaaaa"
			"aaaaaaaaaa",
			"123456789"
			"10111213141516171819"
			"20212223242526272829"
			"30313233343536373839"
			"40414243444546474849"
			"50515253545556575859"
			"60616263646566676869"
			"70717273747576777879"
			"80818283848586878889"
			"90919293949596979899"
			"100101102103104105106107108109")		| // really long test counting from 1 to 109
	0;
	printf("\n%s\n", result ? "failed" : "passed all tests");
	return result;
}
