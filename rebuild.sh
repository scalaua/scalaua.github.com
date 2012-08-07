#!/bin/bash

mv _site/.git _site-git-bak

./site rebuild

mv _site-git-bak _site/.git
