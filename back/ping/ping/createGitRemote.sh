mkdir remote
cd remote
git init
git commit --allow-empty -m "initial commit"
remoteURL=${PWD}/.git
git branch master
git branch bidule
git checkout bidule

cd ..

mkdir repoPush
cd repoPush
git init
git remote add remote ${remoteURL}
touch file1.txt
git add file1.txt
git commit -m "Add file1.txt from java"
git rebase master
git pull remote master
git push --set-upstream remote master

touch file2.txt
touch file3.txt

cd ..

mkdir repoPull
cd repoPull
git init
git remote add remote ${remoteURL}
git commit --allow-empty -m "initial commit"
git pull remote master
git branch --set-upstream-to=remote/master master

cd ..
