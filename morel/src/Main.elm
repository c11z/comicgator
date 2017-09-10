module Main exposing (..)

import Html exposing (..)
import Html.Attributes as Attr
import Html.Events exposing (..)


main : Program Never Model Msg
main =
    Html.beginnerProgram { model = model, view = view, update = update }



-- MODEL


type alias CGForm =
    { email : String
    , isLatest : Bool
    , isReplay : Bool
    , mark : String
    , step : String
    }


type alias Comic =
    { id : String
    , title : String
    , stripCount : Int
    }


type alias Model =
    { email : String
    , isLatest : Bool
    , isReplay : Bool
    , mark : String
    , step : String
    }


model : Model
model =
    Model "" False False "" ""



-- UPDATE


type Msg
    = Email String
    | Mark String
    | Step String
    | ToggleLatest
    | ToggleReplay


update : Msg -> Model -> Model
update msg model =
    case msg of
        Email email ->
            { model | email = email }

        ToggleLatest ->
            { model | isLatest = not model.isLatest }

        ToggleReplay ->
            { model | isReplay = not model.isReplay }

        Mark mark ->
            { model | mark = mark }

        Step step ->
            { model | step = step }



-- VIEW


view : Model -> Html Msg
view model =
    main_
        [ Attr.class "mw6 mw7-ns center pa2 ph5-ns f4 f3-ns" ]
        [ img
            [ Attr.src "images/cg_logo.svg"
            , Attr.class "pb2 pb3-ns"
            , Attr.title "alligator: metal AF"
            ]
            []
        , div [ Attr.class "ma3" ]
            [ label
                [ Attr.class "permanent-marker"
                ]
                [ text "Email"
                , input
                    [ Attr.type_ "email"
                    , Attr.class "w-100 w-80-ns ml3-ns"
                    , Attr.autocomplete True
                    , Attr.placeholder "mr@comicgator.com"
                    , onInput Email
                    ]
                    []
                ]
            ]
        , div [ Attr.class "ma3" ]
            [ label [ Attr.class "permanent-marker mw7-ns" ]
                [ text "Comic"
                , select
                    [ Attr.name "comic"
                    , Attr.class "w-100 w-80-ns ml3-ns"
                    ]
                    [ option
                        [ Attr.value ""
                        , Attr.disabled True
                        , Attr.selected True
                        ]
                        [ text "Select a comic" ]
                    , option [ Attr.value "xkcd" ] [ text "xkcd" ]
                    , option [ Attr.value "smbc" ] [ text "Saturday Morning Breakfast Cereal" ]
                    ]
                ]
            ]
        , div [ Attr.class "ma3" ]
            [ label [ Attr.class "permanent-marker relative db pl4" ]
                [ input
                    [ Attr.type_ "checkbox"
                    , Attr.class "absolute transform-scale left-0 mt2"
                    , onClick ToggleLatest
                    ]
                    []
                , text "Include new comic strips when posted."
                ]
            ]
        , div [ Attr.class "ma3" ]
            [ label [ Attr.class "permanent-marker relative db pl4" ]
                [ input
                    [ Attr.type_ "checkbox"
                    , Attr.class "absolute transform-scale left-0 mt2"
                    , onClick ToggleReplay
                    ]
                    []
                , text "Replay past comic strips."
                ]
            , div
                [ Attr.class "relative db pl4 pt2"
                ]
                [ div [ Attr.class "permanent-marker f6 f4-ns" ]
                    [ text "Start at number"
                    , input
                        [ Attr.disabled (not model.isReplay)
                        , Attr.type_ "number"
                        , Attr.class "ml2 mr2 mw3 tr"
                        , Attr.placeholder "1"
                        , Attr.value model.mark
                        , Attr.min "1"
                        , onInput Mark
                        ]
                        []
                    , text ("/ " ++ "1000")
                    ]
                , div [ Attr.class "permanent-marker f6 f4-ns" ]
                    [ text "Deliver"
                    , input
                        [ Attr.disabled (not model.isReplay)
                        , Attr.type_ "number"
                        , Attr.class " ml2 mr2 mw2-5 tr"
                        , Attr.placeholder "10"
                        , Attr.value model.step
                        , Attr.min "1"
                        , Attr.max "30"
                        , onInput Step
                        ]
                        []
                    , text "per day."
                    ]
                ]
            ]
        , div [ Attr.class "ma3 tc" ]
            [ a
                [ Attr.class "permanent-marker f3 link pointer:hover shadow-hover br3 ba bw1 ph3 pv2 mb2 dib black"
                ]
                [ text "Submit"
                ]
            ]

        -- , viewValidation model
        ]


viewValidation : Model -> Html msg
viewValidation model =
    let
        mark =
            Result.withDefault 0 (String.toInt model.mark)

        step =
            Result.withDefault 0 (String.toInt model.step)

        ( color, message ) =
            if mark > 0 && step > 0 && step < 30 then
                ( "green", "OK" )
            else
                ( "red", "Improbable!" )
    in
        div [ Attr.style [ ( "color", color ) ] ] [ text message ]
