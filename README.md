ScalaUA
=======

# Локальная установка:
Сайт собран с помощью [hakyll][hakyll]'а.
Прежде чем начать, понадобится установить [ghc][ghc] и сам
[hakyll][hakyll].

Проект построен таким образом: в master'е лежит сгенерённый сайт, в
ветке sources -- исходники.
Чтобы произвести локальную установку проекта, сделать нужно следующее:

 `git clone -b sources git@github.com:scalaua/scalaua.github.com.git`

 `cd scalaua.github.com`

 `git clone git@github.com:scalaua/scalaua.github.com.git`

 `mv scalaua.github.com/ _site`

 `ghc --make site.hs`

Далее `./site preview` запустит локальный сервер, а `./site build`
сгенерит в `_site` контент, готовый к коммиту в мастер.

# Добавление новостей, статей, прочего.
Если вы хотите добавить свои статьи, новости, сверстать новый дизайн,
етс, форкните проект, установите его локально, сделайте нужные вам
изменения  пошлите пулл реквесты **в обе** ветки: из директории
`scalaua.github.com` пушить в `sources`, из `scalaua.github.com/_site`
-- в `master`.

[hakyll]:   http://jaspervdj.be/hakyll/
[ghc][ghc]: http://www.haskell.org/ghc/
