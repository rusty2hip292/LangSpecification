
fib :: Integer->Integer
fib 0 = 1
fib 1 = 1
fib n = fib (n - 1) + fib (n - 2)

data FibTree = Leaf Integer | Branch (FibTree) (Integer) (FibTree)
instance Show FibTree where
    show (Leaf f) = show f
    show (Branch l n r) = "(" ++ show l ++ " " ++ show n ++ " " ++ show r ++ ")"

mktree :: Integer->FibTree
mktree 0 = Leaf (fib 0)
mktree 1 = Leaf (fib 1)
mktree n = Branch (mktree (n-2)) (n) (mktree (n-1))

main = do
    print (mktree 9)
