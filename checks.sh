#!/usr/bin/env bash
# Invoked by the shared CICD lint job before Super-linter.
#
# Super-linter does not read .ignore. JSCPD does. Export the same paths as
# FILTER_REGEX_EXCLUDE so every other linter skips them too. Only generated
# and vendored paths belong in .ignore.
set -euo pipefail

if [[ -z "${GITHUB_ENV:-}" ]]; then
	exit 0
fi

ignore_file="${GITHUB_WORKSPACE:-.}/.ignore"
if [[ ! -f "${ignore_file}" ]]; then
	exit 0
fi

regex=""
while IFS= read -r line || [[ -n "${line}" ]]; do
	# Drop comments and surrounding whitespace.
	line="${line%%#*}"
	line="${line#"${line%%[![:space:]]*}"}"
	line="${line%"${line##*[![:space:]]}"}"
	if [[ -z "${line}" ]]; then
		continue
	fi

	directory=false
	if [[ "${line}" == */ ]]; then
		directory=true
		line="${line%/}"
	fi

	# Escape regex metacharacters in the path.
	escaped="$(printf '%s' "${line}" | sed -e 's/[][(){}.+*?^$|\\]/\\&/g')"
	if [[ "${directory}" == true ]]; then
		pattern="(^|/)${escaped}/"
	else
		pattern="(^|/)${escaped}$"
	fi

	if [[ -z "${regex}" ]]; then
		regex="${pattern}"
	else
		regex="${regex}|${pattern}"
	fi
done <"${ignore_file}"

if [[ -z "${regex}" ]]; then
	exit 0
fi

if [[ -n "${FILTER_REGEX_EXCLUDE:-}" ]]; then
	regex="${FILTER_REGEX_EXCLUDE}|${regex}"
fi
printf 'FILTER_REGEX_EXCLUDE=%s\n' "${regex}" >>"${GITHUB_ENV}"
