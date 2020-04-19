
#ifndef LIST_H
#define LIST_H

#include "interfaces/iterable/Iterable.h"
#include <iostream>

template <class T> class List : public Iterable<T> {
public:
	virtual List *add(T t) = 0;

	// throws std::out_of_range if invalid index
	virtual List *add(int index, T t) = 0;

	// throws std::out_of_range if invalid index
	virtual T get(int index) = 0;
	virtual int size() = 0;
	virtual void print() {
		Iterator<T> *itr = this->iterator();
		std::cout << "[";
		bool first = true;
		while(itr->hasNext()) {
			if(first) {
				first = false;
			}else {
				std::cout << ", ";
			}
			std::cout << itr->next();
		}
		std::cout << "]" << std::endl;
	}
	virtual T remove(int index) = 0;
};

#endif
