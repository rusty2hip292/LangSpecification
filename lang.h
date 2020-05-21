
#include <stdint.h>

struct C_Object {
	int32_t class_index; int32_t ref_ctr;
};

struct C_Object_Factory {
	int32_t super_class_index;
	int32_t this_index;
	int32_t low_subclass_index;
	int32_t size_in_bytes;
	void **statics;
	void **constructors;
	void **vtable;
};
