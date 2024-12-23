# How to run Seerene localanalyzer

## 00 Prerequisites

- Local installation of Git version 2.13 or later in the system's PATH. Refer to https://git-scm.com/downloads if you need to download the Git client on your machine.
- The local Git client is configured with access to the repository that is to be analyzed (git clone works on the command line).
- An accessible server that hosts the Git repositories to be analyzed.
- Network access to https://tp.seerene.com.
- A user account with Company Admin permissions is needed for Application creation.
- Ensure localanalyzer is executable `chmod +x localanalyzer`

## 01 Setup

Initialize localanalyzer for JustDonate 

`./localanalyzer init --token <AUTHORIZATION-TOKEN>`

Confige localanalzer by importing config 

`./localanalyzer import config.json`

Check localanalyzer config 

`./localanalyzer check`

## 02 Analysis

Start Anaylsis 

`./localanalyzer analyze`

Upload Results 

`./localanalyzer upload`

