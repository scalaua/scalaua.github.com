{-# LANGUAGE OverloadedStrings #-}
{-# OPTIONS_GHC -fno-warn-unused-do-bind #-}
module Main where

import Control.Arrow ((>>>), arr)
import Control.Monad (forM_)
import Data.List (intercalate)
import Data.Monoid (mappend, mempty)
import Data.Time.Clock (UTCTime)
import Data.Time.Format (parseTime, formatTime)
import System.FilePath
import System.Locale (defaultTimeLocale)
import Text.Pandoc.Shared (HTMLMathMethod(..), ObfuscationMethod(..), WriterOptions(..), defaultWriterOptions)

import Hakyll

main :: IO ()
main = hakyll $ do
    -- Images
    match "images/**" $ do
        route   idRoute
        compile copyFileCompiler
    
    -- Files
    match "files/**" $ do
        route   idRoute
        compile copyFileCompiler
    
    -- Favicon
    match "favicon.ico" $ do
        route   idRoute
        compile copyFileCompiler
    
    -- Robots file
    match "robots.txt" $ do
        route   idRoute
        compile copyFileCompiler
    
    -- JavaScript
    match "js/*" $ do
        route   idRoute
        compile copyFileCompiler
    
    -- Compress CSS
    match "css/*" $ do
        route   idRoute
        compile compressCssCompiler
    
    -- Read templates
    match "templates/*" $ compile templateCompiler
    
    let articleDirs = regex "^(articles|drafts)\\/.+\\.[a-z]+$"
    let allContent  = regex "^(articles|drafts|meetups)\\/.+\\.[a-z]+$"
    let meetupDir   = regex "^(meetups|drafts)\\/.+\\.[a-z]+$"
    
    -- All
    match allContent $ do
        route   $ routeArticle
        compile $ articleCompiler
            >>> arr pageTitle
            >>> arr formatDate
            >>> arr publicationDates
            >>> applyTemplateCompiler "templates/article.html"
            >>> applyTemplateCompiler "templates/default.html"
            >>> relativizeUrlsCompiler
    
    -- Plain text versions of articles
    group "raw" $ do
        match articleDirs $ do
            route   $ routeArticleRaw
            compile $ readPageCompiler
                >>> addDefaultFields
                >>> arr formatDate
                >>> arr publicationDates
                >>> arr (markdownH1 "rawTitle")
                >>> arr (htmlUrl "articleUrl")
                >>> applyTemplateCompiler "templates/raw.txt"
    
    -- Home page
    match  "index.html" $ route idRoute
    create "index.html" $ constA mempty
        >>> arr (setField "pageTitle" "ScalaUA")
        >>> setFieldPageList (newest 10) "templates/short.html" "articles" (articleDirs `mappend` inGroup Nothing)
        >>> applyTemplateCompiler "templates/home.html"
        >>> applyTemplateCompiler "templates/default.html"
        >>> relativizeUrlsCompiler
    
    -- Articles listing
    match  "articles.html" $ route routePage
    create "articles.html" $ constA mempty
        >>> arr (setField "title" "Архив")
        >>> arr pageTitle
        >>> setFieldPageList recentFirst "templates/item.html" "articles" (allContent `mappend` inGroup Nothing)
        >>> applyTemplateCompiler "templates/articles.html"
        >>> applyTemplateCompiler "templates/default.html"
        >>> relativizeUrlsCompiler

    -- Meetups listing
    match  "meetups.html" $ route routePage
    create "meetups.html" $ constA mempty
        >>> arr (setField "title" "Встречи")
        >>> arr pageTitle
        >>> setFieldPageList recentFirst "templates/item.html" "meetups" (meetupDir `mappend` inGroup Nothing)
        >>> applyTemplateCompiler "templates/meetups.html"
        >>> applyTemplateCompiler "templates/default.html"
        >>> relativizeUrlsCompiler
    
    -- Site pages
    forM_ [ "about.md" ] $ \page -> do
        match page $ do
            route   $ routePage
            compile $ pageCompiler'
    
    -- Atom feed
    match  "articles.atom" $ route idRoute
    create "articles.atom" $
        requireAll_ (allContent `mappend` inGroup Nothing) >>> renderAtom feedConfiguration
    
    -- 404 page
    match "404.html" $ do
        route idRoute
        compile $ readPageCompiler
            >>> addDefaultFields
            >>> arr applySelf
            >>> arr pageTitle
            >>> applyTemplateCompiler "templates/default.html"
    
    -- Fin
    return ()

-- | Read a page, add default fields, substitute fields and render with Pandoc.
--
articleCompiler :: Compiler Resource (Page String)
articleCompiler = pageCompilerWith defaultHakyllParserState articleWriterOptions

pageCompiler' :: Compiler Resource (Page String)
pageCompiler' = articleCompiler
    >>> arr pageTitle
    >>> arr publicationDates
    >>> applyTemplateCompiler "templates/page.html"
    >>> applyTemplateCompiler "templates/default.html"
    >>> relativizeUrlsCompiler

-- | Pandoc writer options for articles on ScalaUA.
--
articleWriterOptions :: WriterOptions
articleWriterOptions = defaultWriterOptions
    { writerEmailObfuscation = NoObfuscation
    , writerHTMLMathMethod   = MathML Nothing
    , writerLiterateHaskell  = True
    }

-- | Take a page like @\"/about/notebooks.md\"@ and route it to
-- @\"/about/notebooks\"@, i.e. turn a filename into a drectory.
--
routePage :: Routes
routePage = customRoute fileToDirectory

-- | Drop the date and set the file extension to ".html" when routing articles.
--
routeArticle :: Routes
routeArticle = routeArticleExt ".html"

-- | Drop the date and set the file extension to ".raw" when routing the raw
-- versions of articles.
--
routeArticleRaw :: Routes
routeArticleRaw = routeArticleExt ".txt"

-- | Article routing with a specific file extension.
--
routeArticleExt :: String -> Routes
routeArticleExt ext = customRoute
                    $ flip replaceExtension ext
                    . flip replaceDirectory "articles"
                    . dropDate

-- | Turn an @Identifier@ into a @FilePath@, dropping the date prefix (e.g.
-- @\"2011-04-07-\"@) along the way.
dropDate :: Identifier a -> FilePath
dropDate ident = let file = toFilePath ident
                 in  replaceFileName file (drop 11 $ takeFileName file)

-- | Turn a filename reference into a directory with an index file.
--
fileToDirectory :: Identifier a -> FilePath
fileToDirectory = flip combine "index.html" . dropExtension . toFilePath

-- | ScalaUA date formatting.
--
formatDate :: Page a -> Page a
formatDate = renderDateField "published" "%B %e, %Y" "Date unknown"

-- | Publication and last modified date rendering.
--
publicationDates :: Page a -> Page a
publicationDates page = setField "publicationDates" datesString page
  where
    datesString  = intercalate ". "
                 $ filter ((> 0) . length) [published, updated]
    published    = formatTime' "%B %e, %Y" $ getPublicationDate page
    updated      = formatTime' "Last updated %B %e, %Y" $ getUpdatedDate page

formatTime' :: String -> Maybe UTCTime -> String
formatTime' _      Nothing  = ""
formatTime' format (Just t) = formatTime defaultTimeLocale format t

getPublicationDate :: Page a -> Maybe UTCTime
getPublicationDate page = parseTime defaultTimeLocale "%Y-%m-%d" dateString
  where
    dateString = intercalate "-" $ take 3
               $ splitAll "-" $ takeFileName (getField "path" page)

getUpdatedDate :: Page a -> Maybe UTCTime
getUpdatedDate page =
    parseTime defaultTimeLocale "%Y-%m-%d" (getField "updated" page)

-- | Prefix page titles with "ScalaUA: ".
--
pageTitle :: Page a -> Page a
pageTitle = renderField "title" "pageTitle" ("ScalaUA: " ++)

markdownH1 :: String -> Page a -> Page a
markdownH1 field page = setField field md page
  where
    title = getField "title" page
    line  = replicate (length title) '='
    md    = init $ unlines [title, line]

htmlUrl :: String -> Page a -> Page a
htmlUrl field page = setField field url page
  where
    url = replaceExtension (getField "url" page) ".html"

-- | Take the most recent n articles.
--
newest :: Int -> [Page a] -> [Page a]
newest n = take n . recentFirst

-- | ScalaUA feed metadata.
--
feedConfiguration :: FeedConfiguration
feedConfiguration = FeedConfiguration
    { feedTitle       = "Scala UA"
    , feedDescription = "Scala User Group in Ukraine"
    , feedAuthorName  = "Scala UA"
    , feedRoot        = "http://scalaua.github.com/"
    }
