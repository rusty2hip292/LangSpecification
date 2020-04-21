
int main(int num, char **args) {
	if(num > 0) {
		char *str = args[0];
		return (int) str[0];
	}
	return 1;
}
