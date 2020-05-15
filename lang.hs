
data Stack = EmptyStack | SingleStack a | Stack a b deriving (Show)

main = do
    print (fn 9)
    print (fn' 9)

