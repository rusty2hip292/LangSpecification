
(defun fib (n) (if (< n 2) n (+ (fib (- n 1)) (fib (- n 2)))))

(print (fib 30))

(let ((str "Hello, world!")) (print str))