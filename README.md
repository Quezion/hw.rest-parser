# hw.rest-parser

Practice assignment to implement an app that takes an input records & outputs them in sorted views.

As this isn't a "real production app", this repo's code has some metacommentary about _why_ particular decisions were made.

There are two phases of this app's development:

1. A simple CLI app that takes an input file & outputs three different views of the sorted records
   * Automatically recognizes input file format
   * Renders the three sorted views in order
   * Expects input files to be consistently in one of the three described formats
   * code at https://github.com/Quezion/hw.rest-parser/releases/tag/Step-1

2. A rest HTTP server that receives new records & return sorted variations of them on endpoints
   * Automatically recognizes input record lines at POST /records
   * Returns sorted list of input records (matching CLI's sort) at /color, /name, & /birthdate
   * code at https://github.com/Quezion/hw.rest-parser/releases/tag/Step-2

Note that the final app supports both running in either `$MODE`. Besides renaming, the CLI namespace has been left nearly unchanged from Step-1.

## Usage

Launch a REPL session with your editor of choice (i.e. `M-x cider-jack-in-clj` or `lein repl`)

The app will default to server mode on `lein run`. You may customize this by exporting $env variable `mode` to either `server` or `cli` before running.

### Server

```
lein run
```

You may `export WEBSERVER_PORT`.


#### Example cURLs

```bash
curl -X POST -d "Morris | Tyler | tyler.morris@example.com | orange | 8/6/1951" -vvv localhost:3000/records
curl -X POST -d "Stewart | Dora | dora.stewart@example.com | purple | 9/6/1976" -vvv localhost:3000/records
curl -X POST -d "Pierce, Dennis, dennis.pierce@example.com, black, 9/7/1984" -vvv localhost:3000/records

curl -vvv localhost:3000/records/color | jq
```

```json
[
  {
    "LastName": "Pierce",
    "FirstName": "Dennis",
    "Email": "dennis.pierce@example.com",
    "FavoriteColor": "black",
    "DateOfBirth": "1984-09-07T07:00:00Z"
  },
  {
    "LastName": "Morris",
    "FirstName": "Tyler",
    "Email": "tyler.morris@example.com",
    "FavoriteColor": "orange",
    "DateOfBirth": "1951-08-06T07:00:00Z"
  },
  {
    "LastName": "Stewart",
    "FirstName": "Dora",
    "Email": "dora.stewart@example.com",
    "FavoriteColor": "purple",
    "DateOfBirth": "1976-09-06T07:00:00Z"
  }
]
```

### CLI

See comment at end of `src/hw/rest_parser.clj` for REPL usage examples.

Run directly via `lein` like below.

```bash
export FILEPATHS="data/basic_record.csv,data/basic_record.psv,data/basic_record.ssv"
lein run
```

You may optionally set the column-length (in characters) to be used when rendering the output table.
```
export COLUMN_LENGTH=10
```

#### Example output

```
LastName         FirstName        Email            FavoriteColor    DateOfBirth
====================================================================================
Pierce           Dennis           dennis.pierce... black            9/7/1984
Fitzpatrick      Donna            donna@fitzpat... blue             8/10/1972
Mcclure          Joey             joey.mc@gmail... magenta          3/22/1991
Morris           Tyler            tyler.morris@... orange           8/6/1951
Stewart          Dora             dora.stewart@... purple           9/6/1976
Price            Lucille          lucille.price... silver           1/1/1946
```

### Uberjar

Create via `lein uberjar` and run via `java -jar uber.jar`. Set configuration via $env, see above.

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
