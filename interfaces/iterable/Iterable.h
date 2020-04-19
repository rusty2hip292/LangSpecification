
#ifndef ITERABLE_H
#define ITERABLE_H

#include "interfaces/iterator/Iterator.h"

template <class T> class Iterable {
public:
	virtual Iterator<T> *iterator() = 0;
};

#endif
