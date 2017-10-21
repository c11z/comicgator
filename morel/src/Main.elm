module Main exposing (..)

import Html exposing (..)
import Html.Attributes as Attr
import Html.Events exposing (..)
import Http
import Json.Decode as Decode
import Json.Encode as Encode
import Dict


main : Program Flags Model Msg
main =
    Html.programWithFlags
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }



-- MODEL


type alias Flags =
    { axonUrl : String }


type alias Comic =
    { id : String
    , title : String
    , stripCount : Int
    }


type alias Feed =
    { comicId : String
    , email : String
    , isLatest : Bool
    , isReplay : Bool
    , mark : Maybe Int
    , step : Maybe Int
    }


type alias Model =
    { axonUrl : String
    , comics : List Comic

    -- Form data
    , email : String
    , selectedComic : Maybe Comic
    , isLatest : Bool
    , isReplay : Bool
    , mark : String
    , step : String
    , isFormValid : Bool
    }


init : Flags -> ( Model, Cmd Msg )
init flags =
    ( Model flags.axonUrl [] "" Nothing False False "" "" False
    , getComics flags.axonUrl
    )



-- UPDATE


type Msg
    = SelectComic String
    | NewFeed (Result Http.Error String)
    | NewComics (Result Http.Error (List Comic))
    | Email String
    | Mark String
    | Step String
    | ToggleLatest
    | ToggleReplay
    | Validate


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        SelectComic comicId ->
            let
                comic =
                    List.head (List.filter (\c -> c.id == comicId) model.comics)
            in
                ( { model | selectedComic = comic }, Cmd.none )

        NewFeed (Ok feedLocation) ->
            ( model, Cmd.none )

        NewFeed (Err _) ->
            ( model, Cmd.none )

        NewComics (Ok newComics) ->
            ( { model | comics = newComics }, Cmd.none )

        NewComics (Err _) ->
            ( model, Cmd.none )

        Email email ->
            ( { model | email = email }, Cmd.none )

        ToggleLatest ->
            ( { model | isLatest = not model.isLatest }, Cmd.none )

        ToggleReplay ->
            ( { model | isReplay = not model.isReplay }, Cmd.none )

        Mark mark ->
            ( { model | mark = mark }, Cmd.none )

        Step step ->
            ( { model | step = step }, Cmd.none )

        Validate ->
            case model.selectedComic of
                Just comic ->
                    let
                        feed =
                            Feed
                                comic.id
                                model.email
                                model.isLatest
                                model.isReplay
                                (String.toInt model.mark |> Result.toMaybe)
                                (String.toInt model.step |> Result.toMaybe)
                    in
                        ( model, postFeed model.axonUrl feed )

                Nothing ->
                    ( model, Cmd.none )



-- VIEW


view : Model -> Html Msg
view model =
    main_
        [ Attr.class "mw6 mw7-ns center pa2 ph5-ns f4 f3-ns" ]
        [ div
            [ Attr.class "relative f7 f6-ns sans-serif pv2 bg-washed-red tc hidden" ]
            [ text "Invalid input" ]
        , img
            [ Attr.src "images/cg_logo.svg"
            , Attr.class "pb2 pb3-ns neg-mt2"
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
                    , onInput SelectComic
                    ]
                    ((option
                        [ Attr.value ""
                        , Attr.disabled True
                        , Attr.selected True
                        ]
                        [ text "Select a comic" ]
                     )
                        :: (List.map comicOption model.comics)
                    )
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
                        , Attr.max
                            (case model.selectedComic of
                                Just comic ->
                                    toString comic.stripCount

                                Nothing ->
                                    "1"
                            )
                        , onInput Mark
                        ]
                        []
                    , text
                        ("/ "
                            ++ case model.selectedComic of
                                Just comic ->
                                    toString comic.stripCount

                                Nothing ->
                                    "XXXX"
                        )
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
                , onClick Validate
                ]
                [ text "Submit"
                ]
            ]

        -- , viewValidation model
        ]


comicOption : Comic -> Html msg
comicOption comic =
    option [ Attr.value comic.id ] [ text comic.title ]


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



-- Subscriptions


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-- HTTP


getComics : String -> Cmd Msg
getComics axonUrl =
    let
        url =
            axonUrl ++ "/comics"
    in
        Http.send NewComics (Http.get url decodeComics)


postFeed : String -> Feed -> Cmd Msg
postFeed axonUrl feed =
    let
        url =
            axonUrl ++ "/feeds"

        body =
            Http.jsonBody (encodeFeed feed)

        request =
            Http.request
                { method = "POST"
                , url = url
                , headers = []
                , body = body
                , expect = Http.expectStringResponse (extractHeader "RSS-Feed-Location")
                , timeout = Nothing
                , withCredentials = False
                }
    in
        Http.send NewFeed request


extractHeader : String -> Http.Response String -> Result String String
extractHeader name resp =
    Dict.get name resp.headers
        |> Result.fromMaybe ("header " ++ name ++ " not found")


decodeComics : Decode.Decoder (List Comic)
decodeComics =
    Decode.list
        (Decode.map3 Comic
            (Decode.field "id" Decode.string)
            (Decode.field "title" Decode.string)
            (Decode.field "strip_count" Decode.int)
        )


encodeFeed : Feed -> Encode.Value
encodeFeed feed =
    let
        kv =
            [ ( "email", Encode.string feed.email )
            , ( "comic_id", Encode.string feed.comicId )
            , ( "is_latest", Encode.bool feed.isLatest )
            , ( "is_replay", Encode.bool feed.isReplay )
            , ( "mark"
              , case feed.mark of
                    Just m ->
                        Encode.int m

                    Nothing ->
                        Encode.null
              )
            , ( "step"
              , case feed.step of
                    Just s ->
                        Encode.int s

                    Nothing ->
                        Encode.null
              )
            ]
    in
        kv |> Encode.object
