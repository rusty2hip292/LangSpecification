
#ifndef LINKEDLIST_H
#define LINKEDLIST_H

#include "interfaces/list/List.h"

template <class T> class LinkedList : public List<T> {
private:
	class LinkedListNode {
	public:
		T value;
		LinkedListNode *prev, *next;
		LinkedListNode(T value) {
			this->value = value;
			this->next = 0;
			this->prev = 0;
		}
		virtual ~LinkedListNode() {
			if(this->next) {
				delete this->next;
			}
		}
	};
	class LinkedListIterator : public Iterator<T> {
	private:
		LinkedListNode *_next;
	public:
		LinkedListIterator(LinkedListNode *start) {
			_next = start;
		}
		T next() {
			if(!this->hasNext()) {
				std::out_of_range("no next element");
			}
			T temp = this->_next->value;
			this->_next = this->_next->next;
			return temp;
		}
		bool hasNext() {
			return this->_next;
		}
	};
	int index, _size, roller;
	LinkedListNode *start, *end, *ptr;
public:
	LinkedList() {
		index = 0;
		_size = 0;
		start = 0;
		roller = 0;
	}
	LinkedList *add(T t) {
		LinkedListNode* node = new LinkedListNode(t);
		if(_size++) {
			this->end->next = node;
			node->prev = this->end;
			this->end = node;
		}else {
			start = node; end = node; ptr = node;
		}
		return this;
	}
	LinkedList *add(int index, T t) {
		if(index < 0 || index > this->_size) {
			throw std::out_of_range("index out of bounds");
		}else if(index == this->_size) {
			this->add(t);
		}else {
			LinkedListNode* node = new LinkedListNode(t);
			if(index == 0) {
				this->start->prev = node;
				node->next = this->start;
				this->start = node;
				roller++;
				this->_size++;
			}else {
				LinkedListNode* current = this->getNode(index);
				node->prev = current->prev;
				node->next = current;
				current->prev->next = node;
				current->prev = node;
				this->_size++;
				if(this->roller >= index) {
					this->roller++;
				}
			}
		}
		return this;
	}
	T get(int index) {
		return getNode(index)->value;
	}
	int size() {
		return this->_size;
	}
	virtual ~LinkedList() {
		if(start) {
			delete start;
		}
	}
	Iterator<T> *iterator() {
			return new LinkedListIterator(this->start);
	}
	T remove(int index) {
		LinkedListNode *node = getNode(index);
		if(index == 0) {
			this->start = this->start->next;
		}
		if(index == this->_size - 1) {
			this->end = this->end->prev;
		}
		if(node->prev) {
			node->prev->next = node->next;
		}
		if(node->next) {
			node->next->prev = node->prev;
		}
		T t = node->value;
		if(index == this->roller) {
			// keep roller the same, but advance pointer
			this->ptr = this->ptr->next;
		}else if(index < this->roller) {
			this->roller--;
		}
		this->_size--;
		node->next = 0;
		node->prev = 0;
		delete node;
		return t;
	}
protected:
	LinkedListNode *getNode(int index) {
		if(index < 0 || index > this->_size - 1) {
			throw std::out_of_range("index out of bounds");
		}
		LinkedListNode *node;
		if(index < this->roller) {
			if(this->roller - index < index) {
				node = this->ptr;
				for(int i = this->roller; i > index; i--) {
					node = node->prev;
				}
			}else {
				node = this->start;
				for(int i = 0; i < index; i++) {
					node = node->next;
				}
			}
		}else {
			if(index == this->roller) {
				return ptr;
			}
			if(index - this->roller < this->_size - index - 1) {
				node = this->ptr;
				for(int i = this->roller; i < index; i++) {
					node = node->next;
				}
			}else {
				node = this->end;
				for(int i = this->_size - 1; i > index; i--) {
					node = node->prev;
				}
			}
		}
		return node;
	}
};

#endif
