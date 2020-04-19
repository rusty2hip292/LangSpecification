
#ifndef ITERATOR_H
#define ITERATOR_H

#include <exception>

template <class T> class Iterator {
public:
	virtual bool hasNext() = 0;

	// throws std::logic_error iff not hasNext()
	virtual T next() = 0;
};

#endif
