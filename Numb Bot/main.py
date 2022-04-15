import aiohttp
import discord
import json
import PyAuthGG
from discord.utils import get
from discord.ext import commands
from os import name, system
from base64 import b64decode, b64encode
from numpy import AxisError
from datetime import datetime

#need to add clearlog command

colors = {'white': "\033[1;37m", 'green': "\033[0;32m", 'red': "\033[0;31m", 'yellow': "\033[1;33m"}

def clear():
    if name == 'posix':
        system('clear')
    elif name in ('ce', 'nt', 'dos'):
        system('cls')
    else:
        print("\n") * 120


def ReadConfig():
    with open('[Data]/configs.json', 'r') as f:
        return json.load(f)


def ReplaceValueInJsonb64(filename, key, new_value):
    with open(filename, "r") as jsonFile:
        data = json.load(jsonFile)

    data[key] = b64encode(new_value.encode()).decode()

    with open(filename, "w") as jsonFile:
        json.dump(data, jsonFile)


def ReplaceValueInJson(filename, key, new_value):
    with open(filename, "r") as jsonFile:
        data = json.load(jsonFile)

    data[key] = new_value

    with open(filename, "w") as jsonFile:
        json.dump(data, jsonFile)


def ClearFile(filename):
    with open(filename,'w+',encoding='utf8') as f:
        f.write('')

class Main:
    async def GetUserHWID(self, authkey, username):
        try:
            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/HWID/?type=fetch&authorization={authkey}&user={username}') as response:
                    response_json = await response.json(content_type=None)
                    if response_json['status'] == 'success':
                        hwid = response_json['value']
                        if hwid == '':
                            return
                        else:
                            return hwid
                    else:
                        self.Error('GETUSERHWID','Failed to get hwid')
        except ValueError as v:
            self.Error('VALUEERROR (GETUSERHWID)',v)

    def Log(self, user, command):
        timestamp = str(datetime.now().strftime('%Y-%M-%d %H:%M:%S'))
        print(f"{colors['white']}[{colors['green']}{timestamp}{colors['white']}] USER {colors['yellow']}{str(user)} {colors['white']}USED THE COMMAND {colors['yellow']}{command}")
        with open('[Data]/logs.txt', 'a', encoding='utf8') as f:
            f.write(f'[{timestamp}] USER {str(user)} USED THE COMMAND {command}\n')

    def Error(self,message,error):
        timestamp = str(datetime.now().strftime('%Y-%M-%d %H:%M:%S'))
        print(f"{colors['white']}[{colors['green']}{timestamp}{colors['white']}] {colors['yellow']}{message} {colors['red']}{str(error)}")

    def __init__(self):
        clear()
        self.title = colors['white'] + """
                          ╔═════════════════════════════════════════════════════════════════════╗
                                           ╔═╗╦ ╦╔╦╗╦ ╦ ╔═╗╔═╗  ╔╦╗╔═╗  ╔╗ ╔═╗╔╦╗
                                           ╠═╣║ ║ ║ ╠═╣ ║ ╦║ ╦   ║║║    ╠╩╗║ ║ ║ 
                                           ╩ ╩╚═╝ ╩ ╩ ╩o╚═╝╚═╝  ═╩╝╚═╝  ╚═╝╚═╝ ╩ 
                          ╚═════════════════════════════════════════════════════════════════════╝                                         
        """
        print(self.title)

        self.api_url = "https://api.auth.gg/v1/"
        self.admin_role_id = 'Moderator'
        self.owner_role_id = 'Administrator'
        
        self.bot = commands.Bot(ReadConfig()['prefix'])
        self.bot.remove_command('help')

        self.general_commands = {
#            "✏️ `:expiry username password`": "Users can get their license expiration date.",
            "🔑️ `:extend username password license`": "Users can extend their license with a license key.",
            "🔑️ `:register username password license`": "Users can register their own accounts with a license key.",
#           "⚙️ `prefix <newprefix>`": "Admins can change the bot's prefix.",
            "🕵️ `:getuserinfo <username>`": "Mods can get the user's information",
            "🕵️ `:usercount`": "Mods can check the user count.",
            "🕵️ `:licenseinfo <license>`": "Mods can get license info of a license (used, used by, created at)",
            "🕵️ `:gethwid <username>`": "Mods can get the user's hwid.",
            "❌ `:deluser <username>`": "Admins can delete users from the database.",
            "❌ `:dellicense <license>`": "Admins can delete user's license.",
            "❌ `:clearlog`": "Admins can clear the log.",
            "✏️`:editvar <username> <value>`": "Mods can edit user variables.",
            "✏️`:editrank <username> <rank>`": "Mods can edit the user's rank.",
            "✏️`:changepw <username> <newpassword>`": "Admins can change the user's password.",
            "✏️`:sethwid <username>`": "Admins can set the user's hwid.",
            "👍 `:uselicense <license>`": "Mods can set the license state to used.",
            "👎 `:unuselicense <license>`": "Mods can set the license state to unused.",
            "⏰ `:resethwid <username>`": "Mods can reset the user's hwid.",
#            "🔥 `genlicense <days> <amount> <level> <format> <prefix> <length>`": "Owners can generate license keys."
        }

        @self.bot.event
        async def on_ready():
            print(colors['white'] + '[#] READY!')
            print('')

        @self.bot.event
        async def on_command_error(ctx, error):
            error_str = str(error)
            error = getattr(error, 'original', error)
            if isinstance(error, commands.CommandNotFound):
                return
            elif isinstance(error, commands.CheckFailure):
                self.Error('CHECKFAILURE',error)
            elif isinstance(error, commands.MissingRequiredArgument):
                self.Error('MISSING REQUIRED ARGUMENT',error)
            elif isinstance(error, AxisError):
                self.Error('NOT VALID IMAGE',error)
            elif isinstance(error, discord.errors.Forbidden):
                self.Error('DISCORD FORBIDDEN',error)
            elif "Cannot send an empty message" in error_str:
                self.Error('EMPTY','Couldnt send empty message')         
            else:
                self.Error('UNKNOWN ERROR',error)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def help(ctx):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'help')
            print(ctx.message.content)
            try:
                embed_message = discord.Embed(title='USE COMMANDS IN PREMIUM CHANNEL ONLY!', color=0x0070ff, timestamp=ctx.message.created_at)
                for key in self.general_commands:
                    embed_message.add_field(name=key, value=self.general_commands[key], inline=False)
                await ctx.send(embed=embed_message)
            except Exception as e:
                self.Error('EXCEPTION (HELP)',e)
        
        @self.bot.command(pass_context=True)
        @commands.has_role(self.owner_role_id)
        async def clearlog(ctx):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'clearlog')
            print(ctx.message.content)
            try:
                ClearFile('[Data]/logs.txt')
            except Exception as e:
                self.Error('EXCEPTION (CLEARLOG)',e)
            else:
                embed = discord.Embed(title='CLEARLOG', color=0x00ff00,
                                      description=f'LOG CLEARED\n{ctx.author.mention}')
                await ctx.send(embed=embed)

        #@self.bot.command(pass_context=True)
        #async def getnotified(ctx):
        #    await ctx.message.delete()
        #    member = ctx.message.author
        #    role = get(member.server.roles, name="Notifications")
        #        await bot.add_roles(member, role)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.owner_role_id)
        async def prefix(ctx, newprefix):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'prefix')
            print(ctx.message.content)
            if newprefix is None:
                return
            try:
                ReplaceValueInJson('[Data]/configs.json', 'prefix', newprefix)
            except ValueError as v:
                self.Error('VALUEERROR (PREFIX)',v)
            else:
                self.bot.command_prefix = ReadConfig()['prefix']
                embed = discord.Embed(title='PREFIX', color=0x00ff00,
                                      description=f'PREFIX SET TO ``{newprefix}``\n{ctx.author.mention}')
                await ctx.send(embed=embed)

        # worked but admins could change it to the default role id so all of the users could use the admin commands
        # @bot.command(pass_context=True)
        # async def setadminroleid(ctx,role: discord.Role):
        #    await ctx.message.delete()

        #    if role is None:
        #        return

        #    try:
        #        roleid = role.id
        #        ReplaceValueInJson('[Data]/configs.json','admin_role_id',roleid)
        #    except ValueError as v:
        #        print(colors['yellow']+'JSON Value error at ADMIN ROLE ID CHANGE {0}'.format(colors['red']+str(v)))
        #    else:
        #        embed = discord.Embed(title='ADMINROLEID',color=0x00ff00,description=f'ADMINROLEID SET TO **{str(roleid)}**\n{ctx.author.mention}')
        #        await ctx.send(embed=embed)

        @self.bot.command(pass_context=True)
        async def register(ctx, username, password, license):
                    await ctx.message.delete()
                    self.Log(ctx.message.author, 'register')
                    print(ctx.message.content)
                    if username is None:
                        return

                    if password is None:
                        return

                    if license is None:
                        return

                    aid = ReadConfig()['aid']
                    apikey = ReadConfig()['apikey']
                    secret = ReadConfig()['secret']
                    authkey = ReadConfig()['authkey']
                    App = PyAuthGG.Application(apikey, aid, secret)
                    result = App.Register(license, username, ctx.message.author.id, password)
                    print(result)
                    embed = discord.Embed(color=0x20d420, title=f"account created: {result}")
                    await ctx.send(embed=embed)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def extend(ctx, username, password, license):
                    await ctx.message.delete()
                    self.Log(ctx.message.author, 'extend')
                    print(ctx.message.content)
                    if username is None:
                        return

                    if password is None:
                        return

                    if license is None:
                        return

                    aid = ReadConfig()['aid']
                    apikey = ReadConfig()['apikey']
                    secret = ReadConfig()['secret']
                    authkey = ReadConfig()['authkey']
                    App = PyAuthGG.Application(apikey, aid, secret)
                    result = App.Extend(license, username, password)
                    print(result)
                    embed = discord.Embed(color=0x20d420, title=f"remaning: {result}")
                    await ctx.send(embed=embed)

        @self.bot.command(pass_context=True)
        async def expiry(ctx, username, password):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'expiry')
            print(ctx.message.content)
            if username is None:
                return

            if password is None:
                return

            aid = ReadConfig()['aid']
            apikey = ReadConfig()['apikey']
            secret = ReadConfig()['secret']
            authkey = ReadConfig()['authkey']
            hwid = await self.GetUserHWID(authkey, username)

            payload = {
                'type': 'login',
                'aid': aid,
                'apikey': apikey,
                'secret': secret,
                'username': username,
                'password': password,
                'hwid': hwid
            }

            async with aiohttp.ClientSession() as session:
                async with session.post(self.api_url, data=payload) as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['result'] == 'success':
                            expiry = response_json['expiry']
                            if expiry == '': expiry = 'NOT FOUND'

                            embed = discord.Embed(title='EXPIRY', color=0x00ff00,
                                                  description=f'USER: ``{username}``\nYOUR LICENSE WILL EXPIRE AT: ``{expiry}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['result'] == 'invalid_details':
                            embed = discord.Embed(title='EXPIRY', color=0xff0000,
                                                  description=f'INVALID LOGIN DETAILS ``{username}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['result'] == 'invalid_hwid':
                            embed = discord.Embed(title='EXPIRY', color=0xff0000,
                                                  description=f'INVALID HWID ``{username}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['result'] == 'hwid_updated':
                            embed = discord.Embed(title='EXPIRY', color=0xff0000,
                                                  description=f'HWID UPDATED ``{username}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['result'] == 'time_expired':
                            embed = discord.Embed(title='EXPIRY', color=0xff0000,
                                                  description=f'YOUR LICENSE EXPIRED ``{username}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['result'] == 'failed':
                            print(colors['yellow'] + 'EXPIRY' + colors['red'] + ' Invalid api key')
                        else:
                            embed = discord.Embed(title='EXPIRY', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (EXPIRY)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def getuserinfo(ctx, username):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'getuserinfo')
            print(ctx.message.content)
            if username is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/USERS/?type=fetch&authorization={authkey}&user={username}') as response:
                    try:
                        response_json = await response.json(content_type=None)

                        if response_json['status'] == 'success':
                            email = response_json['email']
                            rank = response_json['rank']
                            hwid = response_json['hwid']
                            variable = response_json['variable']
                            lastlogin = response_json['lastlogin']
                            lastip = response_json['lastip']
                            expiry = response_json['expiry']

                            if email == '': email = 'NOT FOUND'
                            if rank == '': rank = 'NOT FOUND'
                            if hwid == '': hwid = 'NOT FOUND'
                            if variable == '': variable = 'NOT FOUND'
                            if lastlogin == '': lastlogin = 'NOT FOUND'
                            if lastip == '': lastip = 'NOT FOUND'
                            if expiry == '': expiry = 'NOT FOUND'

                            embed = discord.Embed(title='USERINFO', color=0x00ff00,
                                                  description=f'USERNAME: ``{username}``\nEMAIL: ``{email}``\nRANK: ``{rank}``\nHWID: ||``{hwid}``||\nVARIABLE: ``{variable}``\nLASTLOGIN: ``{lastlogin}``\nLASTIP: ||``{lastip}``||\n EXPIRY: ``{expiry}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='USERINFO', color=0xff0000,
                                                  description=f'FAILED TO GET USERINFO USER ``{username}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='USERINFO', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (GETUSERINFO)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.owner_role_id)
        async def deluser(ctx, username):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'deluser')
            print(ctx.message.content)
            if username is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/USERS/?type=delete&authorization={authkey}&user={username}') as response:
                    try:
                        response_json = await response.json(content_type=None)

                        if response_json['status'] == 'success':
                            embed = discord.Embed(title='DELUSER', color=0x00ff00,
                                                  description=f'USER: ``{username}`` DELETED!\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='DELUSER', color=0xff0000,
                                                  description=f'FAILED TO DELETE USER ``{username}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='DELUSER', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (DELUSER)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def editvar(ctx, username, value):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'editvar')
            print(ctx.message.content)
            if username is None:
                return

            if value is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/USERS/?type=editvar&authorization={authkey}&user={username}&value={value}') as response:
                    try:
                        response_json = await response.json(content_type=None)

                        if response_json['status'] == 'success':
                            embed = discord.Embed(title='EDITVAR', color=0x00ff00,
                                                  description=f'USER: ``{username}``\nVARIABLE ``{value}`` ADDED!\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='EDITVAR', color=0xff0000,
                                                  description=f'FAILED TO ADD USER ``{username}`` VARIABLE\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='EDITVAR', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (EDITVAR)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.owner_role_id)
        async def editrank(ctx, username, rank):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'editrank')
            print(ctx.message.content)
            if username is None:
                return

            if rank is None:
                return

            authkey = ReadConfig()['authkey']
            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/USERS/?type=editrank&authorization={authkey}&user={username}&rank={rank}') as response:
                    try:
                        response_json = await response.json(content_type=None)

                        if response_json['status'] == 'success':
                            embed = discord.Embed(title='EDITRANK', color=0x00ff00,
                                                  description=f'USER: ``{username}``\nRANK SET TO ``{rank}``!\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='EDITRANK', color=0xff0000,
                                                  description=f'FAILED TO EDIT ``{username}`` RANK\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='EDITRANK', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (EDITRANK)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.owner_role_id)
        async def changepw(ctx, username, newpassword):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'changepw')
            print(ctx.message.content)
            if username is None:
                return

            if newpassword is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/USERS/?type=changepw&authorization={authkey}&user={username}&password={newpassword}') as response:
                    try:
                        response_json = await response.json(content_type=None)

                        if response_json['status'] == 'success':
                            embed = discord.Embed(title='CHANGEPW', color=0x00ff00,
                                                  description=f'USER: ``{username}`` PASSWORD SET TO ||JUST KIDDING||!\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='CHANGEPW', color=0xff0000,
                                                  description=f'FAILED TO CHANGE USER ``{username}`` PASSWORD\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='CHANGEPW', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (CHANGEPW)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def usercount(ctx):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'usercount')
            print(ctx.message.content)
            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/USERS/?type=count&authorization={authkey}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['status'] == 'success':
                            usernum = response_json['value']
                            embed = discord.Embed(title='USERCOUNT', color=0x00ff00,
                                                  description=f'TOTAL USERS: ``{usernum}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='USERCOUNT', color=0xff0000,
                                                  description=f'FAILED TO GET USERS COUNT\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='USERCOUNT', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (USERCOUNT)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def licenseinfo(ctx, license):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'licenseinfo')
            print(ctx.message.content)
            if license is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/LICENSES/?type=fetch&authorization={authkey}&license={license}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['status'] == 'success':
                            # print(response_json)
                            rank = response_json['rank']
                            used = response_json['used']
                            used_by = response_json['used_by']
                            created = response_json['created']

                            if rank == '': rank = 'NOT FOUND'
                            if used == '': used = 'NOT FOUND'
                            if used_by == '': used_by = 'NOT FOUND'
                            if created == '': created = 'NOT FOUND'

                            embed = discord.Embed(title='LICENSEINFO', color=0x00ff00,
                                                  description=f'LICENSE: ||``{license}``||\nRANK: ``{rank}``\nUSED: ``{used}``\nUSED BY: ``{used_by}``\nCREATED: ``{created}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='LICENSEINFO', color=0xff0000,
                                                  description=f'FAILED TO GET LICENSE INFO ||``{license}``||\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='LICENSEINFO', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (LICENSEINFO)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.owner_role_id)
        async def dellicense(ctx, license):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'dellicense')
            print(ctx.message.content)
            if license is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/LICENSES/?type=delete&license={license}&authorization={authkey}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['status'] == 'success':
                            embed = discord.Embed(title='DELLICENSE', color=0x00ff00,
                                                  description=f'LICENSE DELETED: ||``{license}``||\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='DELLICENSE', color=0xff0000,
                                                  description=f'FAILED TO DELETE LICENSE ||``{license}``||\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='DELLICENSE', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (DELLICENSE)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def uselicense(ctx, license):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'uselicense')
            print(ctx.message.content)
            if license is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/LICENSES/?type=use&license={license}&authorization={authkey}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['status'] == 'success':
                            embed = discord.Embed(title='USELICENSE', color=0x00ff00,
                                                  description=f'LICENSE USED: ||``{license}``||\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='USELICENSE', color=0xff0000,
                                                  description=f'FAILED TO USE LICENSE ||``{license}``||\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='USELICENSE', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (USELICENSE)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def unuselicense(ctx, license):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'unuselicense')
            print(ctx.message.content)
            if license is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/LICENSES/?type=unuse&license={license}&authorization={authkey}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['status'] == 'success':
                            embed = discord.Embed(title='UNUSELICENSE', color=0x00ff00,
                                                  description=f'LICENSE UNUSED: ||``{license}``||\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='UNUSELICENSE', color=0xff0000,
                                                  description=f'FAILED TO UNUSE LICENSE ||``{license}``||\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='UNUSELICENSE', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (UNUSELICENSE)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.owner_role_id)
        async def genlicense(ctx, days, amount, level, format, prefix, length):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'genlicense')
            print(ctx.message.content)
            if days is None:
                return
            if amount is None:
                return
            if level is None or int(level) <= 0:
                return
            if format is None or int(format) > 5:
                return
            if prefix is None:
                return
            if length is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/LICENSES/?type=generate&authorization={authkey}&days={days}&amount={amount}&level={level}&format={format}&prefix={prefix}&length={length}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if 'failed' not in response_json:
                            embed = discord.Embed(title='GENLICENSE', color=0xff0000)
                            description = ''
                            for i in range(int(amount)):
                                license = response_json[f'{str(i)}']
                                description += f'||``{license}``||\n'
                                embed.description = description
                            await ctx.send(embed=embed)
                        elif '"status"' in response_json:
                            if response_json['status'] == 'failed':
                                embed = discord.Embed(title='GENLICENSE', color=0xff0000,
                                                      description=f'FAILED TO GENERATE LICENSE\n{ctx.author.mention}')
                                await ctx.send(embed=embed)
                            else:
                                embed = discord.Embed(title='GENLICENSE', color=0xff0000,
                                                      description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                                await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (GENLICENSE)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def licensecount(ctx):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'licensecount')
            print(ctx.message.content)
            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/LICENSES/?type=count&authorization={authkey}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['status'] == 'success':
                            count = response_json['value']
                            embed = discord.Embed(title='LICENSECOUNT', color=0x00ff00,
                                                  description=f'LICENSE COUNT: ``{count}``\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='LICENSECOUNT', color=0xff0000,
                                                  description=f'FAILED TO GET LICENSE COUNT\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='LICENSECOUNT', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (LICENSECOUNT)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def gethwid(ctx, username):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'gethwid')
            print(ctx.message.content)
            if username is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/HWID/?type=fetch&authorization={authkey}&user={username}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['status'] == 'success':
                            hwid = response_json['value']
                            if hwid == '': hwid = 'NOT FOUND'
                            embed = discord.Embed(title='GETHWID', color=0x00ff00,
                                                  description=f'USER: ``{username}``\nHWID: ||``{hwid}``||\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='GETHWID', color=0xff0000,
                                                  description=f'FAILED TO GET USER ``{username}`` HWID\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='GETHWID', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (GETHWID)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.admin_role_id)
        async def resethwid(ctx, username):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'resethwid')
            print(ctx.message.content)
            if username is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/HWID/?type=reset&authorization={authkey}&user={username}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['status'] == 'success':
                            embed = discord.Embed(title='RESETHWID', color=0x00ff00,
                                                  description=f'USER: ``{username}`` HWID RESET DONE\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='RESETHWID', color=0xff0000,
                                                  description=f'FAILED TO RESET USER ``{username}`` HWID\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='RESETHWID', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (RESETHWID)',v)

        @self.bot.command(pass_context=True)
        @commands.has_role(self.owner_role_id)
        async def sethwid(ctx, username, newhwid):
            await ctx.message.delete()
            self.Log(ctx.message.author, 'sethwid')
            print(ctx.message.content)
            if username is None:
                return

            if newhwid is None:
                return

            authkey = ReadConfig()['authkey']

            async with aiohttp.ClientSession() as session:
                async with session.post(
                        f'https://developers.auth.gg/HWID/?type=set&authorization={authkey}&user={username}&hwid={newhwid}') as response:
                    try:
                        response_json = await response.json(content_type=None)
                        if response_json['status'] == 'success':
                            embed = discord.Embed(title='SETHWID', color=0x00ff00,
                                                  description=f'USER: ``{username}``\nHWID SET TO ||``{newhwid}``||\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        elif response_json['status'] == 'failed':
                            embed = discord.Embed(title='SETHWID', color=0xff0000,
                                                  description=f'FAILED TO SET USER ``{username}`` HWID\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                        else:
                            embed = discord.Embed(title='SETHWID', color=0xff0000,
                                                  description=f'SOMETHING WENT WRONG\n{ctx.author.mention}')
                            await ctx.send(embed=embed)
                    except ValueError as v:
                        self.Error('VALUEERROR (SETHWID)',v)

    def Start(self):
        self.bot.run(ReadConfig()['token'])


if __name__ == '__main__':
    main = Main()
    main.Start()
