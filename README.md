Todosaurus [![Status Zero][status-zero]][andivionian-status-classifier] [![Download](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
==========

<!-- Plugin description -->
**Todosaurus** is an IntelliJ plugin that helps you to manage the TODO comments. It allows quickly creating a new GitHub issue from a TODO comment, and update the comment with the issue number.

For example, if you have a comment `// TODO: Fix this code` in your sources, then the plugin will allow to create an issue linking the line of code including this comment, and update it to `// TODO[#123]: Fix this code`.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Todosaurus"</kbd> >
  <kbd>Install</kbd>

- Manually:

  Download the [latest release](https://github.com/ForNeVeR/Todosaurus/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

How to Use
----------
Currently, plugin only supports one TODO pattern, namely `TODO:` (case-insensitive) that will be converted to `TODO[#<issue-number>]` by the plugin action.

1. Make sure you have enabled the `\btodo\b.*` pattern in the **Settings | Editor | TODO** settings page.

   _(It is enabled by default, so if you didn't touch these settings, then it's enabled for you.)_
2. Check you have a GitHub auth token registered in the **File | Settings | Version Control | GitHub**.
3. Open a Git repository that's available on GitHub (currently, the plugin will auto-detect any GitHub repositories linked by either SSH or HTTPS remotes in the VCS roots of the current project).
4. Open the **TODO** tool window.
5. Open context menu for any TODO item that isn't yet linked to GitHub (i.e. an item like `TODO: something`, with no issue number).
6. Invoke the **Create Issue** action.
7. The dialog will ask you for
   - the account you want to file the issue from,
   - the repository you want to file the issue for,
   - the issue title,
   - the issue text (pattern `${GITHUB_CODE_URL}` will be automatically replaced by the link to the line of code containing the TODO comment).
8. Click the **Create Issue** button. An issue will be created, you'll receive a notification, and the code will be updated to have a link to the issue.

Documentation
-------------
- [License (MIT)][docs.license]
- [Changelog][docs.changelog]
- [Contributor Guide][docs.contributing]
- [Maintainer Guide][docs.maintainer-guide]
- [Code of Conduct (adapted from the Contributor Covenant)][docs.code-of-conduct]

[andivionian-status-classifier]: https://andivionian.fornever.me/v1/#status-zero-
[docs.changelog]: CHANGELOG.md
[docs.code-of-conduct]: CODE_OF_CONDUCT.md
[docs.contributing]: CONTRIBUTING.md
[docs.license]: LICENSE.md
[docs.maintainer-guide]: MAINTAINERSHIP.md
[status-zero]: https://img.shields.io/badge/status-zero-lightgrey.svg
