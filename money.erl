%%%-------------------------------------------------------------------
%%% @author prash
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 10. Jun 2019 2:49 PM
%%%-------------------------------------------------------------------
-module(money).
-author("prash").

%% API
-export([]).
master() ->
  register(master, self),
  customer:getCustomerData().
