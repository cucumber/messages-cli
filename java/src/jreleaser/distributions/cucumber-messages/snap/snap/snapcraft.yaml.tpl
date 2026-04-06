# {{jreleaserCreationStamp}}
name: {{snapPackageName}}
title: Cucumber Messages CLI
version: "{{projectVersion}}"
summary: {{projectDescription}}
description: {{projectLongDescription}}

license: {{projectLicense}}

website: {{projectLinkHomepage}}
source-code: {{projectLinkVcsBrowser}}
contact: {{projectLinkContact}}
issues: {{projectLinkBugTracker}}
donation: {{projectLinkDonation}}

grade: {{snapGrade}}
confinement: {{snapConfinement}}
base: {{snapBase}}
type: app

platforms:
  amd64:
    build-on:
      - amd64
    build-for:
      - amd64
  arm64:
    build-on:
      - arm64
    build-for:
      - arm64

apps:
  {{distributionExecutableName}}:
    command: bin/{{distributionExecutableUnix}}
    completer: bin/{{distributionExecutableUnix}}_bash_completion
    {{#snapHasLocalPlugs}}
    plugs:
      {{#snapLocalPlugs}}
      - {{.}}
      {{/snapLocalPlugs}}
    {{/snapHasLocalPlugs}}
    {{#snapHasLocalSlots}}
    slots:
      {{#snapLocalSlots}}
      - {{.}}
      {{/snapLocalSlots}}
    {{/snapHasLocalSlots}}

{{#snapHasPlugs}}
plugs:
  {{#snapPlugs}}
  {{name}}:
    {{#attrs}}
    {{key}}: {{value}}
    {{/attrs}}
    {{#hasReads}}
    read:
      {{#reads}}
      - {{.}}
      {{/reads}}
    {{/hasReads}}
    {{#hasWrites}}
    write:
      {{#writes}}
      - {{.}}
      {{/writes}}
    {{/hasWrites}}
  {{/snapPlugs}}
{{/snapHasPlugs}}
{{#snapHasSlots}}
slots:
  {{#snapSlots}}
  {{name}}:
    {{#attrs}}
    {{key}}: {{value}}
    {{/attrs}}
    {{#hasReads}}
    reads:
      {{#reads}}
      - {{.}}
      {{/reads}}
    {{/hasReads}}
    {{#hasWrites}}
    writes:
      {{#writes}}
      - {{.}}
      {{/writes}}
    {{/hasWrites}}
  {{/snapSlots}}
{{/snapHasSlots}}
parts:
  {{distributionExecutableName}}:
    plugin: dump
    source: {{distributionUrl}}
    source-checksum: sha256/{{distributionChecksumSha256}}
    override-build: |
      jlink \
        --add-modules io.cucumber.messages.cli \
        --launcher cucumber-messages=io.cucumber.messages.cli/io.cucumber.messages.cli.CucumberMessagesCli \
        --no-man-pages \
        --no-header-files \
        --module-path lib \
        --output working-dir
      cp -r working-dir/* $CRAFT_PART_INSTALL
    build-packages:
      - openjdk-{{distributionJavaVersion}}-jdk

lint:
  ignore:
    - library:
        - lib/*.so
