# hw.rest-parser

Practice assignment to implement a command-line app that takes an input file & outputs sorted views.

As this isn't a "real production app", this repo's code has some metacommentary about _why_ particular decisions were made.

There are two phases of this app's development:

1. A simple CLI app that takes an input file & outputs three different views of the sorted records

2. A rest HTTP server that receives new records & return sorted variations of them on endpoints

## Usage

Launch a REPL session with your editor of choice (i.e. `M-x cider-jack-in-clj` or `lein repl`) See comment at end of `src/hw/rest_parser.clj` for REPL usage examples.

You may also set & run the program directly via `lein` like below.

```bash
export FILEPATH="data/basic_record.csv"
lein run
```

## Test

Run at REPL or use `lein test`

## License

Copyright © 2021 Quest Yarbrough

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
